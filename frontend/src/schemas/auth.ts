import { z } from "zod";
const email = z
  .string()
  .trim()
  .toLowerCase()
  .pipe(z.email("Enter a valid email"));

export const signInSchema = z.object({
  email,
  password: z.string().min(1, "Enter your password"),
});

export const signUpSchema = z
  .object({
    email,
    password: z
      .string()
      .min(10, "At least 10 characters")
      .max(72, "At most 72 characters"),
    confirmPassword: z.string().min(1, "Confirm your password"),
  })
  .refine((data) => data.password === data.confirmPassword, {
    error: "Passwords do not match",
    path: ["confirmPassword"],
  });

export type SignUpValues = z.infer<typeof signUpSchema>;
export type SignInValues = z.infer<typeof signInSchema>;
